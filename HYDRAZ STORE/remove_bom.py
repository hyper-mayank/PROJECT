import os
import codecs

folder = 'src/main/java/com/hydraz/store'
for root, dirs, files in os.walk(folder):
    for f in files:
        if f.endswith('.java'):
            path = os.path.join(root, f)
            with open(path, 'rb') as fp:
                content = fp.read()
            if content.startswith(codecs.BOM_UTF8):
                content = content[3:]
                with open(path, 'wb') as fp:
                    fp.write(content)
                print(f"Removed BOM from {path}")
