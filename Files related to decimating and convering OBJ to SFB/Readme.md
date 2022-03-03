This guide is to generate the decimated objects for the app version, which needs to have decimated objects in the local storage availabe.
As one requirement, you need to have blender-2.80rc2-linux-glibc217-x86_64 unzipped. You can find it here https://download.blender.org/release/Blender2.80/.
Then put the files from this directory along with the unzipped blender to a same folder. Open the terminal in that directory and follow these steps:
1- open objects_obj.txt file. This file contains the address and the decimation ratio of the objects you want to apply decimation on. You can change it based on your needs.
2- open obj_objects folder, then create an empty folder with the name of the object you want to decimate. You can see the sample has plant object.
3-use this command:
blender-2.80rc2-linux-glibc217-x86_64/blender -b -P blenderSimplifyConvertObj.py --  --inm objects_obj.txt
The python program will generate the decimated versions of the objects in a corresponding folder in obj_objects. Then automatically, those files will be converted to sfb and you can see the final results in sfb_objects folder.
Now, the decimated objects are ready for you to transfer them to your phone's local storage.


![Screenshot from 2022-03-02 20-50-45](https://user-images.githubusercontent.com/27611369/156481031-f93e2521-5fb9-49e2-9d7e-25df3e9f39f2.png)
