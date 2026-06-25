export interface PageQuery {
  pageNum: number;
  pageSize: number;
  [key: string]: string | number | undefined | null;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}
