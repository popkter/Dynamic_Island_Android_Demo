# Dynamic_Island_Android_Demo
Dynamic Island Demo On _Android Device



```kotlin
//获取PopWindowManager实例，需要传入Context和LifecycleOwner
override fun onCreate() {
        super.onCreate()
        val layoutInflater =
            this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        windowManager = PopWindowManager(this, this)
        imageSceneBinding = ImageSceneBinding.inflate(layoutInflater)
        tipSceneBinding = TipSceneBinding.inflate(layoutInflater)
        listSceneBinding = RecyclerviewSceneBinding.inflate(layoutInflater)
        //initRecyclerView(listSceneBinding.recyclerView)
    }
//调用PopWindowManager实例的init方法初始化，然后调用Show方法传入View以展示
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager.init()
        lifecycleScope.launch {
            delay(2000)
            windowManager.showView(tipSceneBinding.root)
            delay(2000)
            windowManager.showView(imageSceneBinding.root)
            delay(2000)
            windowManager.showView(listSceneBinding.root)
            delay(2000)
            windowManager.showView(tipSceneBinding.root)
            delay(2000)
            windowManager.showView(imageSceneBinding.root)
            /*delay(2000)
            windowManager.dismiss()*/
        }
        return super.onStartCommand(intent, flags, startId)
    }
```

稍微详细的解析：

[**解析DynamicIslandDemo**](https://nr3o7xky7x.feishu.cn/docx/WAVgdPXuRooHbgxhhBpcgDIOn9g)



