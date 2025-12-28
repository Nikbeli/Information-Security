namespace Desktop_App
{
	partial class FormLogin
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
			this.components = new System.ComponentModel.Container();
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(400, 250);
			this.Text = "Вход в систему";
			this.StartPosition = FormStartPosition.CenterScreen;
			this.FormBorderStyle = FormBorderStyle.FixedSingle;
			this.MaximizeBox = false;

			// Email
			var lblEmail = new Label
			{
				Text = "Email:",
				Location = new System.Drawing.Point(30, 40),
				Size = new System.Drawing.Size(100, 20)
			};
			var txtEmail = new TextBox
			{
				Name = "txtEmail",
				Location = new System.Drawing.Point(30, 65),
				Size = new System.Drawing.Size(340, 27),
				TabIndex = 0
			};

			// Password
			var lblPassword = new Label
			{
				Text = "Пароль:",
				Location = new System.Drawing.Point(30, 110),
				Size = new System.Drawing.Size(100, 20)
			};
			var txtPassword = new TextBox
			{
				Name = "txtPassword",
				Location = new System.Drawing.Point(30, 135),
				Size = new System.Drawing.Size(340, 27),
				TabIndex = 1,
				PasswordChar = '*'
			};

			// Login Button
			var btnLogin = new Button
			{
				Text = "Войти",
				Location = new System.Drawing.Point(30, 180),
				Size = new System.Drawing.Size(340, 35),
				TabIndex = 2,
				TabStop = true
			};
			btnLogin.Click += new EventHandler(this.btnLogin_Click);

			// Add controls
			this.Controls.AddRange(new Control[] { lblEmail, txtEmail, lblPassword, txtPassword, btnLogin });

			// Store references for later use
			this.txtEmail = txtEmail;
			this.txtPassword = txtPassword;
		}

		#endregion
		private TextBox txtEmail;
		private TextBox txtPassword;
	}
}