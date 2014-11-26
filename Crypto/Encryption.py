import base64
from Crypto.Cipher import AES

#PKCS5Padding
BS = 16
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS) 
unpad = lambda s : s[:-ord(s[len(s)-1:])]

def encrypt(text, key):
        text = pad(text)

        cipher = AES.new(key, AES.MODE_ECB)
        return base64.b64encode(cipher.encrypt(text))

def decrypt(encryptedMsg, key):
        enc = base64.b64decode(encryptedMsg)
        cipher = AES.new(key, AES.MODE_ECB)
	paddedMsg = cipher.decrypt(enc)
        return unpad(paddedMsg)
