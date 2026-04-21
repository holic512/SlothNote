import axios from "../../../../../../axios";
import {tokenStore} from "@/pinia/token";

const putAvatar = async (file: File) => {
    const form = new FormData();
    form.append("file", file);
    const token = tokenStore().getUserToken();
    try {
        const response = await axios.post(
            "user/settings/account/avatar",
            form,
            {
                headers: {
                    satoken: token ?? ""
                }
            }
        );
        return { status: response.data.status, url: response.data.data };
    } catch (err) {
        return { status: 500, url: "" };
    }
}

export { putAvatar }
