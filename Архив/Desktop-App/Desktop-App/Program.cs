using Microsoft.Extensions.Configuration;

namespace Desktop_App
{
	internal static class Program
	{
		///  The main entry point for the application.
		[STAThread]
		static void Main()
		{
			// To customize application configuration such as set high DPI settings or default font,
			// see https://aka.ms/applicationconfiguration.
			ApplicationConfiguration.Initialize();

			var configuration = new ConfigurationBuilder().AddJsonFile("appsettings.json", optional: false).Build();

			APIClient.Connect(configuration);

			using (var welcome = new FormWelcome())
			{
				// Показать привественное модальное окно/форму со справочной информацией - поток будет ждать закрытия
				welcome.ShowDialog();
			}

			Application.Run(new FormLogin());
		}
	}
}