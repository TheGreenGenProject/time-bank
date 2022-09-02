package org.timebank.service.calendar

import org.timebank.core.{ActivityId, TimeSlot, UserId}

import java.time.LocalDate


trait CalendarService[M[_]] {

  def book(userId: UserId,
           activityId: ActivityId,
           date: LocalDate,
           timeSlot: TimeSlot): M[Unit]

  def isAvailable(userId: UserId,
                  date: LocalDate,
                  timeSlot: TimeSlot): M[Boolean]

  // Check if the given users has some common availabilities
  def checkAvailabilities(userId: UserId,
                          timeSlots: List[(LocalDate, List[TimeSlot])]): M[List[(LocalDate, List[TimeSlot])]]

  def availabilities(userId: UserId,
                     dates: List[LocalDate]): M[List[(LocalDate, List[TimeSlot])]]

}
