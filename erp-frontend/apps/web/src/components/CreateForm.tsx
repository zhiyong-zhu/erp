import { ModalForm, ProFormDatePicker, ProFormDependency, ProFormDigit, ProFormList, ProFormSelect, ProFormSwitch, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { Card, Col, Row } from "antd";
import type { ReactNode } from "react";

/** 标准字段类型 */
export type FieldType = "text" | "select" | "digit" | "textarea" | "switch" | "datepicker";

/** 选项项 */
export interface FieldOption {
  label: string;
  value: string | number | boolean;
  disabled?: boolean;
}

/** 标准字段配置（text/select/digit/textarea/switch/datepicker 共用） */
export interface StandardField {
  type: FieldType;
  name: string | string[];
  label: ReactNode;
  rules?: any[];
  colSpan?: number;
  options?: FieldOption[];
  placeholder?: string;
  disabled?: boolean;
  showSearch?: boolean;
  min?: number;
  precision?: number;
  autoSize?: { minRows: number; maxRows: number };
  fullWidth?: boolean;
  defaultChecked?: boolean;
  /** 任意额外 ProFormField props 透传 */
  fieldProps?: Record<string, unknown>;
}

/** 依赖型字段配置（映射 ProFormDependency） */
export interface DepField {
  type: "dep";
  watch: string[];
  colSpan?: number;
  render: (values: Record<string, any>) => ReactNode;
}

/** list 行内字段（标准字段或 dep 字段） */
export type ListRowField = StandardField | DepField;

/** list 字段配置（映射 ProFormList） */
export interface ListField {
  type: "list";
  name: string;
  label?: ReactNode;
  creatorButtonText?: string;
  rowFields: ListRowField[];
  rowGutter?: number;
}

export type FieldConfig = StandardField | DepField | ListField;

/** 表单分区（一个 Card） */
export interface FormSection {
  title: ReactNode;
  fields?: FieldConfig[];
  /** 自定义内容插槽（与 fields 二选一或共存） */
  slot?: ReactNode;
  /** Card 右上角额外区域 */
  extra?: ReactNode;
}

export interface CreateFormProps {
  title: ReactNode;
  open: boolean;
  width?: number;
  initialValues?: Record<string, any>;
  onFinish: (values: any) => Promise<boolean>;
  onCancel: () => void;
  sections: FormSection[];
  /** 弹窗 body 最大高度，默认 70vh */
  bodyMaxHeight?: string;
}

function isDepField(f: FieldConfig): f is DepField {
  return f.type === "dep";
}

function isListField(f: FieldConfig): f is ListField {
  return f.type === "list";
}

/** 渲染单个标准字段 */
function renderStandardField(field: StandardField) {
  const commonProps = {
    name: field.name as any,
    label: field.label,
    rules: field.rules
  };

  switch (field.type) {
    case "text":
      return (
        <ProFormText
          {...commonProps}
          placeholder={field.placeholder}
          disabled={field.disabled}
          fieldProps={field.fieldProps as any}
        />
      );
    case "select":
      return (
        <ProFormSelect
          {...commonProps}
          options={field.options}
          placeholder={field.placeholder}
          disabled={field.disabled}
          showSearch={field.showSearch}
          fieldProps={field.fieldProps as any}
        />
      );
    case "digit":
      return (
        <ProFormDigit
          {...commonProps}
          min={field.min}
          fieldProps={{ precision: field.precision, ...(field.fieldProps as any) }}
        />
      );
    case "textarea":
      return (
        <ProFormTextArea
          {...commonProps}
          fieldProps={{ autoSize: field.autoSize ?? { minRows: 2, maxRows: 4 }, ...(field.fieldProps as any) }}
        />
      );
    case "switch":
      return (
        <ProFormSwitch
          {...commonProps}
          fieldProps={{ defaultChecked: field.defaultChecked ?? false, ...(field.fieldProps as any) }}
        />
      );
    case "datepicker":
      return (
        <ProFormDatePicker
          {...commonProps}
          fieldProps={{ style: { width: "100%" }, ...(field.fieldProps as any) }}
        />
      );
    default:
      return null;
  }
}

/** 渲染字段（带 Col 包裹） */
function renderFieldWithCol(field: FieldConfig, defaultColSpan: number) {
  if (isListField(field)) {
    // list 类型不包 Col，整行渲染
    return (
      <ProFormList
        key={`list-${field.name}`}
        name={field.name}
        label={field.label ?? false}
        creatorButtonProps={field.creatorButtonText ? { creatorButtonText: field.creatorButtonText } : undefined}
        itemRender={({ listDom, action }) => (
          <div className="bom-item-row">
            {listDom}
            {action}
          </div>
        )}
      >
        <Row gutter={field.rowGutter ?? 12}>
          {field.rowFields.map((rowField, idx) => {
            const colSpan = "colSpan" in rowField ? rowField.colSpan : defaultColSpan;
            return (
              <Col key={`rowfield-${idx}`} xs={24} md={colSpan}>
                {isDepField(rowField)
                  ? <ProFormDependency name={rowField.watch}>{(values) => rowField.render(values)}</ProFormDependency>
                  : renderStandardField(rowField)}
              </Col>
            );
          })}
        </Row>
      </ProFormList>
    );
  }

  const colSpan = "colSpan" in field ? field.colSpan : defaultColSpan;
  const isFullRow = colSpan === 24;

  if (isDepField(field)) {
    return (
      <Col key={`dep-${field.watch.join("-")}`} xs={24} md={colSpan}>
        <ProFormDependency name={field.watch}>{(values) => field.render(values)}</ProFormDependency>
      </Col>
    );
  }

  return (
    <Col key={`field-${typeof field.name === "string" ? field.name : field.name.join("-")}`} xs={24} md={isFullRow ? 24 : colSpan}>
      {renderStandardField(field)}
    </Col>
  );
}

export function CreateForm({
  title,
  open,
  width = 720,
  initialValues,
  onFinish,
  onCancel,
  sections,
  bodyMaxHeight = "70vh"
}: CreateFormProps) {
  return (
    <ModalForm
      title={title}
      open={open}
      width={width}
      initialValues={initialValues}
      modalProps={{ destroyOnClose: true, onCancel, bodyStyle: { maxHeight: bodyMaxHeight, overflowY: "auto", paddingRight: 8 } }}
      onFinish={onFinish}
    >
      {sections.map((section, idx) => (
        <Card
          key={`section-${idx}`}
          size="small"
          title={section.title}
          className="form-section-card"
          styles={{ body: { padding: 16 } }}
          extra={section.extra}
        >
          {section.fields && section.fields.length > 0 ? (
            <Row gutter={16}>
              {section.fields.map((field) => renderFieldWithCol(field, 12))}
            </Row>
          ) : null}
          {section.slot}
        </Card>
      ))}
    </ModalForm>
  );
}
