import { MIME_TYPES } from "@/shared/constants";
import { DiagramViewer } from "../hooks/useDiagramViewer";

export const exportAsSVG = async (viewer: DiagramViewer, filename: string = "diagram"): Promise<void> => {
  const { svg } = await viewer.saveSVG?.() ?? { svg: "" };
  const blob = new Blob([svg], { type: MIME_TYPES.SVG });
  downloadFile(URL.createObjectURL(blob), `${filename}.svg`);
};

export const exportAsPNG = async (viewer: DiagramViewer, filename: string = "diagram"): Promise<void> => {
  const { svg } = await viewer.saveSVG?.() ?? { svg: "" };
  const canvas = document.createElement("canvas");
  const ctx = canvas.getContext("2d");
  const img = new Image();

  return new Promise((resolve, reject) => {
    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      ctx?.drawImage(img, 0, 0);
      const dataUrl = canvas.toDataURL(MIME_TYPES.PNG);
      downloadFile(dataUrl, `${filename}.png`);
      resolve();
    };
    img.onerror = reject;
    img.src = `data:${MIME_TYPES.SVG};base64,` + btoa(unescape(encodeURIComponent(svg)));
  });
};

export const downloadFile = (
  content: string,
  fileName: string,
  contentType?: string
): void => {
  const link = document.createElement("a");

  if (contentType) {
    const blob = new Blob([content], { type: contentType });
    link.href = contentType.startsWith(MIME_TYPES.PNG)
      ? content
      : URL.createObjectURL(blob);

    link.download = fileName;
    link.click();

    if (!contentType.startsWith(MIME_TYPES.PNG)) {
      URL.revokeObjectURL(link.href);
    }
  } else {
    link.href = content;
    link.download = fileName;
    link.click();
    if (!content.startsWith("data:")) {
      URL.revokeObjectURL(content);
    }
  }
};
