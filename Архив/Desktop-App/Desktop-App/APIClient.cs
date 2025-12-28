using Microsoft.Extensions.Configuration;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;

namespace Desktop_App
{
	public static class APIClient
	{
		private static readonly HttpClient _client = new();

		public static string ErrorMessage { get; private set; } = string.Empty;

		/// Устанавливаем базовый адрес API из конфигурации
		public static void Connect(IConfiguration configuration)
		{
			var baseUrl = configuration["IPAddress"]!;

			if (string.IsNullOrWhiteSpace(baseUrl))
				throw new InvalidOperationException("Конфигурационный параметр 'IPAddress' не найден или пуст.");

			_client.BaseAddress = new Uri(baseUrl);
			_client.DefaultRequestHeaders.Clear();
			_client.DefaultRequestHeaders.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
		}

		// Универсальный метод GET-запроса
		public static T? GetRequestAsync<T>(string url)
		{
			try
			{
				var response = _client.GetAsync(url);
				var result = response.Result.Content.ReadAsStringAsync().Result;

				if (response.Result.IsSuccessStatusCode)
				{
					return JsonConvert.DeserializeObject<T>(result);
				}
				else
				{
					throw new Exception($"Server returned error / Ошибка запроса: {result}");
				}

			}
			catch (HttpRequestException ex)
			{
				throw new Exception("Не удалось подключиться к серверу. Проверьте соединение и адрес.", ex);
			}
		}

		/// POST-запрос с объектом модели
		public static void PostRequest<T>(string requestUrl, T model)
		{
			var json = JsonConvert.SerializeObject(model);
			var data = new StringContent(json, Encoding.UTF8, "application/json");

			var response = _client.PostAsync(requestUrl, data);

			var result = response.Result.Content.ReadAsStringAsync().Result;

			if (!response.Result.IsSuccessStatusCode)
			{
				throw new Exception(result);
			}
		}
	}
}
