package aspect.domain

/*
{
  "_id": "<md5 of query>",
  "query": "<query>",

  ? "userIds": [],
  ? "projectIds": [],
  ? "targetIds": [],

  "found": {
    "min": {
      "id": "<id of min found tweet>",
      "time": <time of min found tweet>
    },
    "max": {
      "id": "<id of max found tweet>",
      "maxTime": <time of max found tweet>
    }
  },

  "pending": {
    "min: {
      "id": "<id of min pending tweet>",
      "time": "<time of min pending tweet>",
    },
    "max": {
      "id": "<id of max pending tweet>",
      "time": "<time of max pending tweet>"
    }
  },

  "forward": {
    "last": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of last execution>,
      "finishTime: <start time of last execution>,
      "error": {
        "count": <amount of last errors if last operation is failed>,
        "message": "<last error message>"
      }
    },

    "nextTime": <time of next forward processing>,

    "current": {
      "id" : "<correlation id of current execution if it's pending>",
      "startTime" : <start time of current execution if it's pending>
    },


    "successInterval" : <current value of dynamic success interval>,
    "errorInterval" : <current value of dynamic error interval>

  },
  "backward": {

    "last": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of last execution>,
      "finishTime: <start time of last execution>,
      "error": {
        "count": <amount of last errors if last operation is failed>,
        "message": "<last error message>"
      }
    },

    "nextTime": <time of next backward processing>,

    "current": {
      "id": "<correlation id of current execution if it's pending>",
      "startTime": <start time of current execution if it's pending>
    },

    "errorInterval" : <current value of dynamic error interval>,

    "completed" : <indicates if the backward search completed>
  },
  "disabled" : <indicates if the query disabled>
}

*/
