var $taskList = $("#task-list-container");
var $lastUpdate = $("#last-updated");

getTasks();

$("#refresh-btn").click(getTasks);

setInterval(getTasks, 10000);

function getTasks() {
	$.getJSON(apiUrl + "/public/guest/tasks?page=0&size=5&sortBy=createdDateTime&sortOrder=desc")
		.done(function (tasks) {
			if (tasks.length === 0) {
				$taskList.addClass('hide');
			} else {
				$taskList.removeClass('hide');
				$table = $taskList.find('tbody');
				$table.empty();
				tasks.forEach(function (task) {
					$table.append($("<tr>")
						.append($("<td>").text(task.name))
						.append($("<td>").text(parseOperation(task.operation)))
						.append($("<td>").text(task.status).css('color', getColorByStatus(task.status)))
						.append($("<td>").text(parseDate(task.createdDateTime)))
						.append($("<td>").text(parseDate(task.startDateTime)))
						.append($("<td>").text(parseDate(task.endDateTime))))
				});
			}
			$lastUpdate.text("Updated: " + parseDate(Date.now()));
		});
}

function parseDate(date) {
	if (!date) return '';
	return (new Date(date)).toLocaleString()
}

function getColorByStatus(status) {
	switch(status) {
		case 'SUCCEEDED':
			return '#00e676';
		case 'RUNNING':
			return '#00b0ff';
		case 'QUEUED':
			return '#ffea00';
		case 'FAILED':
			return '#ff1744';
	}
}

function parseOperation(value) {
	return value.replace('_', ' ');
}
