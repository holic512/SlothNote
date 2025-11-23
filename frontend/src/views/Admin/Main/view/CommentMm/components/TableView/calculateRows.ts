export const calculateRows = (minHeight: number, stepHeight: number) => {
  const windowHeight = window.innerHeight;
  if (windowHeight <= minHeight) return 10;
  const extraRows = Math.floor((windowHeight - minHeight) / stepHeight);
  return 10 + extraRows;
}