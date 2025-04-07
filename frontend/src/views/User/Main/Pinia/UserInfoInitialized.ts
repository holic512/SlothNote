// 控制 用户的个性化设置
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
            const response = await axios.get(
                "user/account/hasProfile"
            )
            return response.data.data
        },

        // 用户完成了信息的初始化
        completeInfoInitialized() {
            this.isInfoInitialized = true;
        }
    },
    persist: true,
});