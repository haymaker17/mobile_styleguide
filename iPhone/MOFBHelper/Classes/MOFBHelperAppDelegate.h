//
//  MOFBHelperAppDelegate.h
//  MOFBHelper
//
//  Created by Dan Grigsby on 6/9/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MOFBHelperViewController;

@interface MOFBHelperAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    MOFBHelperViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MOFBHelperViewController *viewController;

@end

