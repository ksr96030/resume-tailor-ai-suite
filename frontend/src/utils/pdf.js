import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

export async function downloadTextAsPDF({ text, filename = 'tailored-resume.pdf' }) {
  // Simple multi-page text PDF
  const doc = new jsPDF({ unit: 'pt', format: 'a4' });
  const maxWidth = 500; // wrap width
  const lineHeight = 17;
  const marginX = 50, marginY = 60;

  const lines = doc.splitTextToSize(text || '', maxWidth);
  let y = marginY;

  lines.forEach((line, idx) => {
    if (y > 770) {
      doc.addPage();
      y = marginY;
    }
    doc.text(line, marginX, y);
    y += lineHeight;
  });

  doc.save(filename);
}


