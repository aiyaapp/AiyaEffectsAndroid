import os

libs = [("AyBeauty", "beauty"),
        ("AyCore", "core"),
        ("AyEffect", "aiyagift"),
        ("AyFaceTrack", "aiyatrack"),
        ("AyShortVideoEffect", "shortvideo")
]
for l in libs:
    targets = ["arm64-v8a", "armeabi-v7a", "x86"]
    for t in targets:
        src = l[0] + "/libs/" + t + "/*.so"
        dst = "output/" + l[1] + "/" + t
        src_dir = l[0] + "/libs/" + t
        os.system("ls " + src_dir)
        os.system("ls " + dst)
        cmd = "cp " + src + " " + dst
        print(cmd)
        os.system(cmd)
