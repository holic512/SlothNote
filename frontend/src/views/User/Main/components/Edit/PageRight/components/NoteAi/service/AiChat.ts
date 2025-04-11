import { defineStore } from 'pinia';
import { ref } from 'vue';
import axios from 'axios';

export interface ChatMessage {
    id: string;
    role: 'user' | 'assistant';
    content: string;
    timestamp: number;
}

export const useAiChatStore = defineStore('aiChat', () => {
    const messages = ref<ChatMessage[]>([]);
    const loading = ref(false);
    const currentMessageId = ref<string | null>(null);
    const abortController = ref<AbortController | null>(null);
    const selectedText = ref<string>(''); // 存储从编辑器选中的文本

    /**
     * 设置选中的文本
     */
    const setSelectedText = (text: string) => {
        selectedText.value = text;
    };

    /**
     * 获取选中的文本
     */
    const getSelectedText = () => {
        return selectedText.value;
    };

    /**
     * 添加用户消息
     */
    const addUserMessage = (content: string) => {
        const message: ChatMessage = {
            id: `user-${Date.now()}`,
            role: 'user',
            content,
            timestamp: Date.now()
        };
        messages.value.push(message);
        console.log('添加用户消息:', message);
    };

    /**
     * 添加助手(AI)消息
     */
    const addAssistantMessage = (content: string) => {
        const message: ChatMessage = {
            id: `assistant-${Date.now()}`,
            role: 'assistant',
            content,
            timestamp: Date.now()
        };
        messages.value.push(message);
        currentMessageId.value = message.id;
        console.log('添加AI回复:', message);
    };

    /**
     * 更新助手消息内容
     */
    const updateAssistantMessage = (content: string) => {
        if (currentMessageId.value) {
            const message = messages.value.find(m => m.id === currentMessageId.value);
            if (message) {
                console.log('更新消息:', message.id, '角色:', message.role, '内容长度:', content.length);
                message.content = content;
            }
        }
    };

    /**
     * 发送解释请求
     */
    const explainText = async () => {
        if (!selectedText.value) {
            return;
        }
        
        try {
            loading.value = true;
            // 添加用户消息
            addUserMessage(`请解释以下文本：\n${selectedText.value}`);
            // 先创建一个空的AI回复消息
            addAssistantMessage('');

            // 创建新的 AbortController
            abortController.value = new AbortController();

            const response = await fetch('http://localhost:8080/user/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: `请解释以下文本：\n${selectedText.value}`,
                    user: '123456' // 替换为实际的用户ID
                }),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            if (!reader) {
                throw new Error('无法获取响应流');
            }

            // 用于累积完整内容
            let fullContent = '';
            const decoder = new TextDecoder();

            // 读取流
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // 解码新收到的数据
                const chunk = decoder.decode(value, { stream: true });
                
                // 分割并处理每一行
                const lines = chunk.split('\n');
                
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        try {
                            const jsonData = line.slice(5).trim();
                            if (jsonData && jsonData !== '[DONE]') {
                                const data = JSON.parse(jsonData);
                                
                                // 处理从后端接收的数据
                                if (data.role === 'assistant' && data.content) {
                                    // 这是带有角色信息的响应
                                    fullContent += data.content;
                                    updateAssistantMessage(fullContent);
                                } else if (data.choices && data.choices[0] && data.choices[0].delta) {
                                    // 处理原始的科大讯飞响应格式
                                    const deltaContent = data.choices[0].delta.content || '';
                                    fullContent += deltaContent;
                                    updateAssistantMessage(fullContent);
                                }
                            }
                        } catch (e) {
                            console.error('处理响应数据失败:', e, line);
                        }
                    }
                }
            }
            
        } catch (error) {
            console.error('解释请求出错:', error);
            updateAssistantMessage('解释请求处理出错，请重试。');
        } finally {
            loading.value = false;
            abortController.value = null;
        }
    };

    /**
     * 发送润色请求
     */
    const polishText = async () => {
        if (!selectedText.value) {
            return;
        }
        
        try {
            loading.value = true;
            // 添加用户消息
            addUserMessage(`请润色以下文本：\n${selectedText.value}`);
            // 先创建一个空的AI回复消息
            addAssistantMessage('');

            // 创建新的 AbortController
            abortController.value = new AbortController();

            const response = await fetch('http://localhost:8080/user/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: `请润色以下文本：\n${selectedText.value}`,
                    user: '123456' // 替换为实际的用户ID
                }),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            if (!reader) {
                throw new Error('无法获取响应流');
            }

            // 用于累积完整内容
            let fullContent = '';
            const decoder = new TextDecoder();

            // 读取流
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // 解码新收到的数据
                const chunk = decoder.decode(value, { stream: true });
                
                // 分割并处理每一行
                const lines = chunk.split('\n');
                
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        try {
                            const jsonData = line.slice(5).trim();
                            if (jsonData && jsonData !== '[DONE]') {
                                const data = JSON.parse(jsonData);
                                
                                // 处理从后端接收的数据
                                if (data.role === 'assistant' && data.content) {
                                    // 这是带有角色信息的响应
                                    fullContent += data.content;
                                    updateAssistantMessage(fullContent);
                                } else if (data.choices && data.choices[0] && data.choices[0].delta) {
                                    // 处理原始的科大讯飞响应格式
                                    const deltaContent = data.choices[0].delta.content || '';
                                    fullContent += deltaContent;
                                    updateAssistantMessage(fullContent);
                                }
                            }
                        } catch (e) {
                            console.error('处理响应数据失败:', e, line);
                        }
                    }
                }
            }
            
        } catch (error) {
            console.error('润色请求出错:', error);
            updateAssistantMessage('润色请求处理出错，请重试。');
        } finally {
            loading.value = false;
            abortController.value = null;
        }
    };

    /**
     * 发送生成简介请求
     */
    const generateSummary = async () => {
        if (!selectedText.value) {
            return;
        }
        
        try {
            loading.value = true;
            // 添加用户消息
            addUserMessage(`请为以下文本生成简介：\n${selectedText.value}`);
            // 先创建一个空的AI回复消息
            addAssistantMessage('');

            // 创建新的 AbortController
            abortController.value = new AbortController();

            const response = await fetch('http://localhost:8080/user/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: `请用简洁的语言为以下文本生成一段不超过200字的简介，概括其主要内容：\n${selectedText.value}`,
                    user: '123456' // 替换为实际的用户ID
                }),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            if (!reader) {
                throw new Error('无法获取响应流');
            }

            // 用于累积完整内容
            let fullContent = '';
            const decoder = new TextDecoder();

            // 读取流
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // 解码新收到的数据
                const chunk = decoder.decode(value, { stream: true });
                
                // 分割并处理每一行
                const lines = chunk.split('\n');
                
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        try {
                            const jsonData = line.slice(5).trim();
                            if (jsonData && jsonData !== '[DONE]') {
                                const data = JSON.parse(jsonData);
                                
                                // 处理从后端接收的数据
                                if (data.role === 'assistant' && data.content) {
                                    // 这是带有角色信息的响应
                                    fullContent += data.content;
                                    updateAssistantMessage(fullContent);
                                } else if (data.choices && data.choices[0] && data.choices[0].delta) {
                                    // 处理原始的科大讯飞响应格式
                                    const deltaContent = data.choices[0].delta.content || '';
                                    fullContent += deltaContent;
                                    updateAssistantMessage(fullContent);
                                }
                            }
                        } catch (e) {
                            console.error('处理响应数据失败:', e, line);
                        }
                    }
                }
            }
            
        } catch (error) {
            console.error('生成简介请求出错:', error);
            updateAssistantMessage('生成简介请求处理出错，请重试。');
        } finally {
            loading.value = false;
            abortController.value = null;
        }
    };

    /**
     * 发送消息到AI
     */
    const sendMessage = async (content: string) => {
        try {
            loading.value = true;
            
            // 如果没有用户消息，则添加
            if (!messages.value.length || messages.value[messages.value.length - 1].role !== 'user') {
                addUserMessage(content);
                // 先创建一个空的AI回复消息
                addAssistantMessage('');
            }

            // 创建新的 AbortController
            abortController.value = new AbortController();

            const response = await fetch('http://localhost:8080/user/ai/chat', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: content,
                    user: '123456' // 替换为实际的用户ID
                }),
                signal: abortController.value.signal
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const reader = response.body?.getReader();
            if (!reader) {
                throw new Error('无法获取响应流');
            }

            // 用于累积完整内容
            let fullContent = '';
            const decoder = new TextDecoder();

            // 读取流
            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // 解码新收到的数据
                const chunk = decoder.decode(value, { stream: true });
                console.log("收到数据块:", chunk);
                
                // 分割并处理每一行
                const lines = chunk.split('\n');
                
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        try {
                            const jsonData = line.slice(5).trim();
                            if (jsonData && jsonData !== '[DONE]') {
                                const data = JSON.parse(jsonData);
                                console.log("解析的数据:", data);
                                
                                // 处理从后端接收的数据
                                if (data.role === 'assistant' && data.content) {
                                    // 这是带有角色信息的响应
                                    fullContent += data.content;
                                    updateAssistantMessage(fullContent);
                                } else if (data.choices && data.choices[0] && data.choices[0].delta) {
                                    // 处理原始的科大讯飞响应格式
                                    const deltaContent = data.choices[0].delta.content || '';
                                    fullContent += deltaContent;
                                    updateAssistantMessage(fullContent);
                                }
                            }
                        } catch (e) {
                            console.error('处理响应数据失败:', e, line);
                        }
                    }
                }
            }
            
            console.log("流式响应结束，完整内容:", fullContent);
            
        } catch (error: any) {
            if (error.name === 'AbortError') {
                console.log('对话已终止');
                updateAssistantMessage(messages.value.find(m => m.id === currentMessageId.value)?.content + ' [用户已终止]');
            } else {
                console.error('发送消息失败:', error);
                updateAssistantMessage('抱歉，获取回复失败。');
            }
        } finally {
            loading.value = false;
            abortController.value = null;
        }
    };

    /**
     * 终止当前对话
     */
    const stopChat = async () => {
        if (abortController.value) {
            abortController.value.abort();
            try {
                await axios.post('http://localhost:8080/user/ai/stop', {
                    user: '123456' // 替换为实际的用户ID
                });
            } catch (error) {
                console.error('终止对话失败:', error);
            }
        }
    };

    /**
     * 清空所有对话记录
     */
    const clearMessages = () => {
        messages.value = [];
        currentMessageId.value = null;
        selectedText.value = ''; // 清空选中的文本
    };

    return {
        messages,
        loading,
        selectedText,
        setSelectedText,
        getSelectedText,
        sendMessage,
        explainText,
        polishText,
        generateSummary,
        stopChat,
        clearMessages
    };
});