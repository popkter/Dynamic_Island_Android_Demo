package com.example.customizeview
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @功能: 用于和Service通信
 * @User Lmy
 * @Creat 4/16/21 8:37 AM
 * @Compony 永远相信美好的事情即将发生
 */
object ViewModleMain : ViewModel() {
    //悬浮窗口创建 移除  基于无障碍服务
    var isShowWindow = MutableLiveData<Boolean>()
    //悬浮窗口创建 移除

    var isShowSuspendWindow = MutableLiveData<Boolean>()

    //悬浮窗口显示 隐藏
    var isVisible = MutableLiveData<Boolean>()

}
