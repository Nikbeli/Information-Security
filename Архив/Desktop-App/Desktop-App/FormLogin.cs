using Microsoft.Extensions.Configuration;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Desktop_App
{
	public partial class FormLogin : Form
	{
		public FormLogin()
		{
			InitializeComponent();
		}

		private void btnLogin_Click(object sender, EventArgs e)
		{
			string email = txtEmail.Text.Trim();
			string password = txtPassword.Text;

			if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
			{
				MessageBox.Show("Пожалуйста, заполните все поля.", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Warning);
				return;
			}

			try
			{
				var loginModel = new { email = email, password = password };
				APIClient.PostRequest("/api/v1/auth/login", loginModel);
				MessageBox.Show("Код подтверждения отправлен на ваш email.", "Успех", MessageBoxButtons.OK, MessageBoxIcon.Information);
				// Здесь можно перейти к форме ввода OTP
			}
			catch (Exception ex)
			{
				MessageBox.Show($"Ошибка входа:\n{ex.Message}", "Ошибка", MessageBoxButtons.OK, MessageBoxIcon.Error);
			}
		}
	}
}
