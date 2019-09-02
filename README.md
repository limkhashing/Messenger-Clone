# Messenger-Clone
A Kotlin version of Messenger following Brian lesson

https://www.youtube.com/playlist?list=PL0dzCUj1L5JE-jiBHjxlmXEkQkum_M3R-

This is an incomplete versoin of messenger
Lack of:
1. Notification
2. Sorting the messenge based on timestamp

## Issues
The code will not work at big scale or in production because it is reading from database a lot of messages with no pagination.
Also, If multiple users are sending messages, reloading the recycler view each time also will be really expensive.
