Feature: User Registration, Login, and Deletion by Admin

  Scenario Outline: Register, get token, access all users, and delete user by Admin
    Given the user does not already exist with email "<user_email>"
    When the user registers with the following details:
      | name      | email             | gender | password  |
      | <user_name> | <user_email>     | <user_gender> | <user_password> |
    Then the user is successfully registered with email "<user_email>"

    When the user logs in with email "<user_email>" and password "<user_password>"
    Then the user should receive a valid JWT token

    Given the user has a valid JWT token
    When the user requests to get all users using the token
    Then the response should contain the user details

    Given the admin user exists with email "<admin_email>"
    Then the admin gets the token with email "<admin_email>" and password "<admin_password>"
    When the admin deletes the user with email "<user_email>"
    Then the user with email "<user_email>" should be deleted successfully

    Examples:
      | user_name  | user_email         | user_gender | user_password | admin_email       | admin_password |
      | testuser   | test@mykare.com    | male        | password1     | admin@example.com | adminpassword  |

