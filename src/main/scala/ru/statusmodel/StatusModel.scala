package ru.statusmodel

object TaskStatus extends Enumeration {
  lazy val COMPLETED_SUCCESSFULLY, IN_PROGRESS, WAITING_FOR_EXECUTION, DEFERRED, DELETE = Value
}

object WomanStatus extends Enumeration {
  lazy val BAN_TEMPORARILY, BAN_FOREVER, ACTIVE, NOT_USE, AWAITING_CLARIFICATION, RESERVE, NEED_VALIDATION, ERROR_PROXY = Value
}

object ProxyStatus extends Enumeration {
  lazy val ACTIVE, DELETE = Value
}

object TypeOfMailing extends Enumeration {
  lazy val EVEN_DAYS, ODD_DAYS, EVERY_DAY = Value
}

object FriendListStatus extends Enumeration {
  lazy val EXPECTS, DONE = Value
}
