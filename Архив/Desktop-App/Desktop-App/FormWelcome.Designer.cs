namespace Desktop_App
{
	partial class FormWelcome
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.IContainer components = null;

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing && (components != null))
			{
				components.Dispose();
			}
			base.Dispose(disposing);
		}

		#region Windows Form Designer generated code

		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			btnNext = new Button();
			lblStatus = new Label();
			SuspendLayout();
			// 
			// btnNext
			// 
			btnNext.BackColor = Color.FromArgb(0, 122, 204);
			btnNext.FlatAppearance.BorderSize = 0;
			btnNext.FlatStyle = FlatStyle.Flat;
			btnNext.Font = new Font("Segoe UI", 10.2F, FontStyle.Bold);
			btnNext.ForeColor = Color.White;
			btnNext.Location = new Point(424, 500); // Ниже начальной позиции
			btnNext.Margin = new Padding(2);
			btnNext.Name = "btnNext";
			btnNext.Size = new Size(120, 36);
			btnNext.TabIndex = 1;
			btnNext.Text = "Далее →";
			btnNext.UseVisualStyleBackColor = false;
			btnNext.Click += btnNext_Click;
			btnNext.MouseEnter += btnNext_MouseEnter;
			btnNext.MouseLeave += btnNext_MouseLeave;
			// 
			// lblStatus
			// 
			lblStatus.AutoSize = true;
			lblStatus.Font = new Font("Segoe UI", 9F, FontStyle.Italic);
			lblStatus.ForeColor = Color.Gray;
			lblStatus.Location = new Point(450, 45);
			lblStatus.Margin = new Padding(2, 0, 2, 0);
			lblStatus.Name = "lblStatus";
			lblStatus.Size = new Size(77, 20);
			lblStatus.TabIndex = 2;
			lblStatus.Text = "Загрузка...";
			lblStatus.Visible = false;
			// 
			// FormWelcome
			// 
			AutoScaleDimensions = new SizeF(8F, 20F);
			AutoScaleMode = AutoScaleMode.Font;
			BackColor = Color.White;
			ClientSize = new Size(966, 600); // Увеличенная высота по умолчанию
			Controls.Add(lblStatus);
			Controls.Add(btnNext);
			FormBorderStyle = FormBorderStyle.FixedSingle;
			Margin = new Padding(2);
			MaximizeBox = false;
			MinimizeBox = false;
			Name = "FormWelcome";
			StartPosition = FormStartPosition.CenterScreen;
			Text = "Добро пожаловать";
			ResumeLayout(false);
			PerformLayout();
		}

		#endregion
		private System.Windows.Forms.Button btnNext;
		private System.Windows.Forms.Label lblStatus;
	}
}