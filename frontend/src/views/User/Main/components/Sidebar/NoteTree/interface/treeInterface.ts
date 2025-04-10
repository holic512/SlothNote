export interface Tree {
    id: number;
    label: string;
    type: string;
    children?: Tree[] | null;
    avatar?: string;
    uniqueId?: string;
}