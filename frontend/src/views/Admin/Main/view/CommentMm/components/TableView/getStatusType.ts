export const getStatusMsg = (isDeleted: boolean) => isDeleted ? '已删除' : '有效'
export const getStatusType = (isDeleted: boolean) => isDeleted ? 'danger' : 'success'