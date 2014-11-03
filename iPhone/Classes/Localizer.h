//
//  Localizer.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 9/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface Localizer : NSObject {
	NSMutableDictionary *configDict;
	//NSMutableDictionary *defaultDict;
}

@property (strong) NSMutableDictionary *configDict;
//@property (retain) NSMutableDictionary *defaultDict;

+(Localizer*)sharedInstance;
+(NSString *)getViewTitle:(NSString *)theViewName;
+(NSString *)getLocalizedText:(NSString *)localConstant;
+(BOOL)hasLocalizedText:(NSString *)localConstant;
+(NSString *)getPreferredLanguage;
@end
