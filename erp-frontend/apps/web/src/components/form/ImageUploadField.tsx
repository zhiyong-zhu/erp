import { UploadOutlined } from "@ant-design/icons";
import { App, Button, Empty, Space, Tag, Typography, Upload } from "antd";
import { useState } from "react";
import { buildImageUrl, uploadProductImage } from "../../api/product";

const { Text } = Typography;

export function ImageUploadField({ value, onChange }: {
  value: string[];
  onChange: (urls: string[]) => void;
}) {
  const { message } = App.useApp();
  const [uploading, setUploading] = useState(false);

  async function handleUpload(file: File) {
    setUploading(true);
    try {
      const uploaded = await uploadProductImage(file);
      onChange([...value, uploaded.url]);
      message.success("图片上传成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "图片上传失败");
    } finally {
      setUploading(false);
    }
    return false;
  }

  return (
    <div className="form-subsection">
      <div className="form-subsection-head">
        <Text strong>产品图片</Text>
        <Upload beforeUpload={handleUpload} showUploadList={false} multiple>
          <Button size="small" icon={<UploadOutlined />} loading={uploading}>上传图片</Button>
        </Upload>
      </div>
      {value.length > 0 ? (
        <Space wrap size={[8, 8]}>
          {value.map((url) => (
            <div key={url} style={{ position: "relative", display: "inline-block" }}>
              <a href={buildImageUrl(url)} target="_blank" rel="noreferrer">
                <img src={buildImageUrl(url)} alt="产品图片" style={{ width: 80, height: 80, objectFit: "cover", borderRadius: 4, border: "1px solid #f0f0f0" }} />
              </a>
              <Tag
                closable
                onClose={() => onChange(value.filter((item) => item !== url))}
                style={{ position: "absolute", top: -8, right: -8, fontSize: 10, lineHeight: "16px", padding: "0 4px" }}
              />
            </div>
          ))}
        </Space>
      ) : (
        <Text type="secondary" style={{ fontSize: 13 }}>暂无图片</Text>
      )}
    </div>
  );
}

export function EmptyImageUploadField() {
  return <Empty description="暂无图片" />;
}
