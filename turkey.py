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

turkey_id = "VG-TRK-004"

if turkey_id in metrics_map and turkey_id in calcs_map:
    print("--- RAW METRICS ---")
    print(json.dumps(metrics_map[turkey_id], indent=2))
    print("\n--- CALCULATED RESTOCK VARIABLES ---")
    print(json.dumps(calcs_map[turkey_id], indent=2))
else:
    print(f"SKU {turkey_id} not found in responses.")
