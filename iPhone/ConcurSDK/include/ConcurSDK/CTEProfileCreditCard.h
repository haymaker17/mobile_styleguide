//
//  CTEProfileCreditCard.h
//  ConcurSDK
//
//  Created by Ray Chi on 12/15/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>



@interface CTEProfileCreditCard : NSObject

@property (nonatomic,strong,readonly) NSString *creditCardNo;
@property (nonatomic,strong,readonly) NSString *name;

/**
 *  We assume expire date is the 15th day of each month, mid night, GMT time
    e.g 06/18 means 2018/06/15 00:00:00 GMT+0
    Using 15th in case some one mess up the time zone when import this NSDate to a datepicker, 
    even they chose time zone other then GMT, then can still get the right month and year.
 */
@property (nonatomic,strong,readonly) NSDate   *expDate;
@property (nonatomic,strong,readonly) NSString *cvsCode;
@property (nonatomic,strong,readonly) NSString *creditCardType;
@property (nonatomic,strong,readonly) NSString *expMonth;
@property (nonatomic,strong,readonly) NSString *expYear;

/**
 *  Initialize class from server json
 */
- (id)initWithServerJson:(NSDictionary *)json;

/**
 *  Initialize class from user's input, before you call this function, make sure your input is Valid Credit Card function
 *
 *  @param name         string
 *  @param creditCardNo valid credit card number
 *  @param date         GMT timezone
 *  @param cvsCode      3/4 digits
 */
- (id)initWithName:(NSString *)name
      creditCardNo:(NSString *)creditCardNo
           expDate:(NSDate *)date
           cvsCode:(NSString *)cvsCode;

/**
 *  Detect whether the input month/year string is valid expire date
 *
 *  @param str   e.g "10/18","10/2018"
 *  @return true if the string is valid, otherwise false
 */
- (BOOL)isValidExpireDateWithYearMonthString:(NSString *)str;

/**
 *  Use Luhn Algorithm to check whether the input string is a valid credit card number.
 */
+ (BOOL)isCreditCardNumberValid:(NSString *)creditCardNo;

@end
