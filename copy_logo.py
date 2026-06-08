import os
from PIL import Image

src_path = r'C:\Users\Navahas\.gemini\antigravity\brain\0cb51bd1-79cc-4554-8146-03415ca6474d\media__1778179393035.png'
out_path = r'C:\Users\Navahas\Desktop\Euroformac\TFG\app\src\main\res\drawable\ic_logo_stockcalzado.png'

img = Image.open(src_path)
print("Mode:", img.mode)
img.save(out_path)
print("Logo copied to drawable.")
