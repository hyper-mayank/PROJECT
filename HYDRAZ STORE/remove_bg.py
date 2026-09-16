import os
import rembg
from PIL import Image

img_dir = r'C:\Users\Admin\Documents\PROJECTS\HYDRAZ STORE\src\main\resources\web\img'
keywords = ['key', 'rank', 'badge', 'coins', 'crates', 'section']

for filename in os.listdir(img_dir):
    if filename.endswith('.png'):
        if any(kw in filename.lower() for kw in keywords):
            input_path = os.path.join(img_dir, filename)
            output_path = os.path.join(img_dir, 'temp_' + filename)
            
            print(f'Processing {filename}...')
            
            try:
                with open(input_path, 'rb') as i:
                    input_data = i.read()
                    
                output_data = rembg.remove(input_data)
                
                with open(output_path, 'wb') as o:
                    o.write(output_data)
                    
                # Replace the original with the transparent one
                os.replace(output_path, input_path)
                print(f'Successfully removed background for {filename}')
            except Exception as e:
                print(f'Failed to process {filename}: {e}')
