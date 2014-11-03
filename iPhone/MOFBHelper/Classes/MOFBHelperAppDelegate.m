//
//  MOFBHelperAppDelegate.m
//  MOFBHelper
//
//  Created by Dan Grigsby on 6/9/09.
//  Copyright __MyCompanyName__ 2009. All rights reserved.
//

#import "MOFBHelperAppDelegate.h"
#import "MOFBHelperViewController.h"

@implementation MOFBHelperAppDelegate

@synthesize window;
@synthesize viewController;


- (void)applicationDidFinishLaunching:(UIApplication *)application {    
    
    // Override point for customization after app launch    
    [window addSubview:viewController.view];
    [window makeKeyAndVisible];
}


- (void)dealloc {
    [viewController release];
    [window release];
    [super dealloc];
}


@end
