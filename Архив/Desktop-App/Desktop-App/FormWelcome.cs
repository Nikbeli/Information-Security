using Desktop_App.Models;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Desktop_App
{
	public partial class FormWelcome : Form
	{
		public FormWelcome()
		{
			InitializeComponent();
			LoadInfoAsync();
		}

		private void LoadInfoAsync()
		{
			btnNext.Enabled = false;
			lblStatus.Visible = true;

			try
			{
				var info = APIClient.GetRequestAsync<InfoResponse>("api/v1/info/about");
				RenderInfo(info!);
			}
			catch (Exception ex)
			{
				MessageBox.Show($"Ошибка загрузки:\n{ex.Message}", "Ошибка",
					MessageBoxButtons.OK, MessageBoxIcon.Error);
			}
			finally
			{
				lblStatus.Visible = false;
				btnNext.Enabled = true;
			}
		}

		private void RenderInfo(InfoResponse info)
		{
			var panel = new TableLayoutPanel
			{
				Location = new Point(50, 80),
				Size = new Size(850, 400), // Увеличил ширину и высоту
				AutoSize = true,
				AutoSizeMode = AutoSizeMode.GrowAndShrink,
				ColumnCount = 2,
				BackColor = SystemColors.Window,
				Padding = new Padding(25), // Увеличил отступы
				BorderStyle = BorderStyle.FixedSingle
			};

			panel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 30)); // Немного уменьшил левую колонку
			panel.ColumnStyles.Add(new ColumnStyle(SizeType.Percent, 70)); // Увеличил правую колонку

			void AddRow(string label, string value)
			{
				// Заголовок (левая колонка)
				var lbl = new Label
				{
					Text = label,
					Font = new Font("Segoe UI", 11F, FontStyle.Bold), // Увеличил шрифт
					TextAlign = ContentAlignment.TopRight,
					AutoSize = false,
					Size = new Size(240, 50), // Увеличил высоту
					Margin = new Padding(0, 8, 15, 8), // Увеличил отступы
					ForeColor = Color.FromArgb(40, 40, 40),
					UseMnemonic = false
				};

				// Значение (правая колонка) - теперь с фиксированной высотой но большей
				var val = new Label
				{
					Text = value,
					Font = new Font("Segoe UI", 11F, FontStyle.Regular), // Увеличил шрифт
					TextAlign = ContentAlignment.TopLeft,
					AutoSize = false,
					Size = new Size(500, 50), // Увеличил ширину и высоту
					Margin = new Padding(15, 8, 0, 8), // Увеличил отступы
					ForeColor = Color.FromArgb(60, 60, 60),
					UseMnemonic = false
				};

				// Добавляем строку в таблицу
				panel.RowCount++;
				panel.RowStyles.Add(new RowStyle(SizeType.Absolute, 66f)); // Фиксированная высота строки

				panel.Controls.Add(lbl, 0, panel.RowCount - 1);
				panel.Controls.Add(val, 1, panel.RowCount - 1);
			}

			// Добавляем данные
			AddRow("👤 Автор:", $"{info.Author} ({info.Group})");
			AddRow("🧪 Работа:", info.LabNumber);
			AddRow("🔒 Тема:", info.Topic);
			AddRow("⚙️ Алгоритмы:", info.Algorithms);
			AddRow("🛡️ Политика паролей:", info.PasswordPolicy);

			Controls.Add(panel);

			// Перемещаем кнопку ниже
			btnNext.Location = new Point(
				(ClientSize.Width - btnNext.Width) / 2,
				panel.Bottom + 30
			);

			// Увеличиваем размер формы чтобы все вместилось
			this.Height = btnNext.Bottom + 80;
		}


		private void btnNext_Click(object sender, EventArgs e)
		{
			DialogResult = DialogResult.OK;
			Close();
		}


		private void btnNext_MouseEnter(object sender, EventArgs e) =>
			btnNext.BackColor = Color.FromArgb(0, 102, 170);

		private void btnNext_MouseLeave(object sender, EventArgs e) =>
			btnNext.BackColor = Color.FromArgb(0, 122, 204);
	}
}
