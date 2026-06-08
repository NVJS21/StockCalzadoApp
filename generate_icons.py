import os
from PIL import Image, ImageOps, ImageDraw, ImageChops

def trim_white_bg(im):
    bg = Image.new(im.mode, im.size, im.getpixel((0,0)))
    diff = ImageChops.difference(im, bg)
    diff = ImageChops.add(diff, diff, 2.0, -100)
    bbox = diff.getbbox()
    if bbox:
        return im.crop(bbox)
    return im

def make_icons():
    src_path = r'C:\Users\Navahas\.gemini\antigravity\brain\0cb51bd1-79cc-4554-8146-03415ca6474d\media__1778179393035.png'
    out_dir = r'C:\Users\Navahas\Desktop\Euroformac\TFG\app\src\main\res'
    
    img = Image.open(src_path).convert('RGBA')
    
    bg = Image.new('RGBA', img.size, (255, 255, 255, 255))
    img = Image.alpha_composite(bg, img)
    
    img = trim_white_bg(img)
    
    w, h = img.size
    size = max(w, h)
    padding = int(size * 0.15)
    new_size = size + padding * 2
    
    square_img = Image.new('RGBA', (new_size, new_size), (255, 255, 255, 255))
    square_img.paste(img, ((new_size - w) // 2, (new_size - h) // 2))
    
    round_img = square_img.copy()
    mask = Image.new('L', (new_size, new_size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, new_size, new_size), fill=255)
    round_img.putalpha(mask)
    
    sizes = {
        'mdpi': 48,
        'hdpi': 72,
        'xhdpi': 96,
        'xxhdpi': 144,
        'xxxhdpi': 192
    }
    
    for dpi, s in sizes.items():
        folder = os.path.join(out_dir, f'mipmap-{dpi}')
        if not os.path.exists(folder):
            os.makedirs(folder)
            
        sq_resized = square_img.resize((s, s), Image.Resampling.LANCZOS)
        sq_resized.save(os.path.join(folder, 'ic_launcher.png'), 'PNG')
        
        rd_resized = round_img.resize((s, s), Image.Resampling.LANCZOS)
        rd_resized.save(os.path.join(folder, 'ic_launcher_round.png'), 'PNG')
        
    print('Icons generated successfully.')

make_icons()
