export const calculateRows = (minHeight: number, stepHeight: number): number => {
    const height = window.innerHeight;
    if (height <= minHeight) return 10;
    const extraRows = Math.floor((height - minHeight) / stepHeight);
    return 10 + extraRows;
}