import axios from "../../../../../../axios";

const putAvatar = async (file: File) => {
    const form = new FormData();
    form.append("file", file);
    try {
        const response = await axios.post(
            "user/settings/account/avatar",
            form,
            { headers: { "Content-Type": "multipart/form-data" } }
        );
        return { status: response.data.status, url: response.data.data };
    } catch (err) {
        return { status: 500, url: "" };
    }
}

export { putAvatar }