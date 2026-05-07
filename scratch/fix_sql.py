path = 'src/main/resources/seed_comprehensive_test_data.sql'
with open(path, 'r') as f:
    content = f.read()

new_content = content.replace('THEN 880 ELSE', 'THEN 201 ELSE')

with open(path, 'w') as f:
    f.write(new_content)

print('SUCCESS: Updated 880 to 201 in seed file.')
