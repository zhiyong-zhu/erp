export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface PageQuery {
  pageNum: number;
  pageSize: number;
}
