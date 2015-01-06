//
//  CCViewController.h
//  ConcurMobile
//
//  Created by laurent mery on 20/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
/* generique view controller
 keep it VERY LIGHT
 role is
 -- import all basic class used by all view controller
 (before include another one, must ask himself if is it really necessary ?)
 -- get name controller (usefull for log)
 -- //TODO: manage lost connection
*/

#import <UIKit/UIKit.h>

/*
 * colors concur definition
 * recommandation: avoir to designate color by a colorName
 * design Name by its behavior, role like "textFormLabel" instead "lightBlueColor"
 */
#import "UIColor+ConcurColor.h"


/*
 * spinner manager
 * draw a modal spinner all over the screen
 */
#import "WaitViewController.h"

/*
 * designe predefined formed like button, view, label..
 * and bring size estimate method over label, view...
 */
#import "UIView+Styles.h"

/*
 * extend nsstring with localized method for example
 * example: [@"Save" localize]
 */
#import "NSStringAdditions.h"


/*
 * format log message
 */
#import "CCFlurryLogs.h"



@interface CCViewController : UIViewController

/*
 * is a public property to be set by parent view
 */
@property (copy, nonatomic) NSString *callerViewName;

/*
 * static is important because, sometimes, we need to get this value before its instantiation
 * viewName must be design by module-typeView
 *    module as Expense, Travel or Request
 *    typeView as userList, Digest, Header...
 */
+(NSString*)viewName;

-(NSString*)viewName;



@end
