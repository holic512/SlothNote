/**
 * @file UserInfoInitializedStore
 * @project SlothNote
 * @module 用户端 / 资料初始化状态
 * @description 管理用户资料初始化引导的持久化状态与后端资料检查。
 * @logic 1. 从本地持久化读取初始化状态；2. 通过账号资料接口判断资料是否已填写；3. 完成引导后写入 Pinia 持久化状态。
 * @dependencies Pinia: defineStore, API: user/settings/account/userAllProfile
 * @index_tags 用户资料初始化, Pinia, userAllProfile, 引导弹窗
 * @author holic512
 */
import {defineStore} from "pinia";
import axios from "@/axios";

export const useUserInfoInitialized = defineStore('UserInfoInitialized', {
    state: () => ({
        // 用户的个人信息是否初始化
        isInfoInitialized: false,


    }),
    actions: {
        // 查询用户信息是否初始化
        async checkUserInfo(): Promise<boolean> {
            if (localStorage.getItem('hasCompletedInit') === 'true') return true;

            const response = await axios.get(
                "user/settings/account/userAllProfile"
            )
            const profile = response.data.data;
            return Boolean(
                profile?.nickname ||
                profile?.gender ||
                profile?.age ||
                profile?.contactInfo ||
                profile?.bio ||
                profile?.avatar
            )
        },

        // 用户完成了信息的初始化
        completeInfoInitialized() {
            this.isInfoInitialized = true;
        }
    },
    persist: true,
});
