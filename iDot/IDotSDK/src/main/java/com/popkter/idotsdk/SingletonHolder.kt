package com.popkter.idotsdk

/**
 * 有参的单例模式基类
 * [From] https://www.jianshu.com/p/3fc4bd25fdb2
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T =
        instance ?: synchronized(this) {
            instance ?: creator!!(arg).apply {
                instance = this
            }
        }

}