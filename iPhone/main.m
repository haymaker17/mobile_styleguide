//
//  main.m
//  ConcurMobile
//
//  Created by Paul Kramer on 10/27/09.
//  Copyright Concur 2009. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <ADEUMInstrumentation/ADEUMInstrumentation.h>

int main(int argc, char *argv[]) {
    
    [ADEumInstrumentation initWithKey:@"AD-AAB-AAA-FUF"];
    @autoreleasepool {
        int retVal = UIApplicationMain(argc, argv, nil, nil);
        return retVal;
    }
}
