$(document).ready(
		function() {
			$('a[name=captchaLink]').click(
					function() {
						$(this).prev().attr(
								'src',
								'@{Register.captcha(randomID)}&t='	+ Math.random());
					});
		})
