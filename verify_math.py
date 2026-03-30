import urllib.request, json, math

url_metrics = "http://localhost:8080/api/v1/inventory/all-metrics"
url_calc = "http://localhost:8080/api/v1/inventory/calculate-restock-all"

req_metrics = urllib.request.Request(url_metrics)
with urllib.request.urlopen(req_metrics) as response:
    metrics = json.loads(response.read().decode())

req_calc = urllib.request.Request(url_calc)
with urllib.request.urlopen(req_calc) as response:
    calcs = json.loads(response.read().decode())

metrics_map = {m['skuId']: m for m in metrics}
calcs_map = {c['skuId']: c for c in calcs}

errors = 0
for sku, c in calcs_map.items():
    if sku not in metrics_map: continue
    m = metrics_map[sku]

    longTermAvg = m.get('avgSales30d') or 0.0
    shortTermAvg = m.get('avgSales3d') or 0.0
    
    momentum = 0.0
    forecast = longTermAvg
    if longTermAvg > 0:
        momentum = (shortTermAvg - longTermAvg) / longTermAvg
    elif shortTermAvg > 0:
        momentum = 1.0
        forecast = shortTermAvg
    
    seasonalityFactor = m.get('seasonalityFactor') or 1.0
    adjustedDemand = forecast * (1 + 0.8 * momentum) * seasonalityFactor
    
    stdDev = m.get('stdDev30d') or 0.0
    cv = 0.0 if longTermAvg == 0 else stdDev / longTermAvg
    dynamicZ = 1.65 * (1 + cv)
    
    leadTimeDays = m.get('leadTimeDays') or 0
    safetyStock = dynamicZ * stdDev * math.sqrt(leadTimeDays)
    
    shelfLifeDays = m.get('shelfLifeDays') or 365
    maxSellableQty = adjustedDemand * shelfLifeDays
    
    reviewPeriodDays = m.get('reviewPeriodDays') or 0
    targetStock = (adjustedDemand * reviewPeriodDays) + safetyStock
    
    safeTargetStock = min(targetStock, maxSellableQty)
    
    currentStock = m.get('currentStock') or 0
    incomingStock = m.get('incomingStock') or 0
    
    netRequirement = safeTargetStock - (currentStock + incomingStock)
    rawOrderQty = max(0, netRequirement)
    
    finalOrderQty = math.ceil(rawOrderQty)
    if finalOrderQty > 0:
        caseSize = m.get('caseSize') or 1
        if finalOrderQty % caseSize != 0:
            finalOrderQty += (caseSize - (finalOrderQty % caseSize))
        moq = m.get('supplierMoq') or 1
        finalOrderQty = max(finalOrderQty, moq)
        
    calc_orderQty = c.get('orderQuantity')
    if finalOrderQty != calc_orderQty:
        print(f"Mismatch for {sku}: Calculated {finalOrderQty}, Expected {calc_orderQty}")
        errors += 1

if errors == 0:
    print("All calculations verified successfully!")
else:
    print(f"Found {errors} errors.")
