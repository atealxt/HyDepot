-- convert `log_time` from varchar to datetime 

ALTER TABLE `log`
	ADD COLUMN `log_time1` DATETIME NULL DEFAULT NULL AFTER `log_time`;

UPDATE LOG t SET t.log_time1 = STR_TO_DATE(t.log_time, '%Y-%m-%dT%H:%i:%s.%f+0000');

ALTER TABLE `log`
	DROP COLUMN `log_time`;

ALTER TABLE `log`
	CHANGE COLUMN `log_time1` `log_time` DATETIME NULL DEFAULT NULL AFTER `raw`;

ALTER TABLE `log`
	ADD INDEX `log_time` (`log_time`);

ALTER TABLE `log`
	ADD COLUMN `log_date` DATE NULL DEFAULT NULL AFTER `log_time`;

UPDATE LOG t SET t.log_date = DATE(t.log_time);

ALTER TABLE `log`
	DROP INDEX `doc_id`,
	ADD INDEX `doc_id` (`doc_id`, `log_date`);
