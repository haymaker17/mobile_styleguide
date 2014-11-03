//
//  LoginHelpTopic.m
//  ConcurMobile
//
//  Created by charlottef on 12/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "LoginHelpTopic.h"
#import "Localizer.h"
#import "Config.h"

@implementation LoginHelpTopic

@synthesize navBarTitle, heading, body;

+(NSArray*) topics
{
    NSMutableArray *topicsArray = [[NSMutableArray alloc] init];
    
//    if (![Config isGov] && [self deviceIsUsingEnglish]) {
//        // This is a rather awkward solution. Really this class should know how to handle this data...
//        [topicsArray addObject:[[LoginHelpTopic alloc] initWithNavBarTitle:nil heading:[Localizer getLocalizedText:@"Watch setup video"] body:nil]];
//    }

    NSString *body = [Localizer getLocalizedText:@"Help for User Name"];
    if ([Config isGov]) {
        body = [Localizer getLocalizedText:@"Help for User Name - Gov"];
    }
    [topicsArray addObject:[[LoginHelpTopic alloc] initWithNavBarTitle:[Localizer getLocalizedText:@"More Help"] heading:[Localizer getLocalizedText:@"Username and Password"] body:body]];


//    body = [Localizer getLocalizedText:@"Help for Password"];
//    if ([Config isGov]) {
//        body = [Localizer getLocalizedText:@"Help for Password - Gov"];
//    }
//    [topicsArray addObject:[[LoginHelpTopic alloc] initWithNavBarTitle:[Localizer getLocalizedText:@"Password"] heading:[Localizer getLocalizedText:@"Don't know your password or PIN?"] body:body]];

    return topicsArray;
}

+(bool) deviceIsUsingEnglish
{
    if ([[[NSLocale preferredLanguages] objectAtIndex:0] hasPrefix:@"en"]) {
        return true;
    }
    return false;
}

-(id) initWithNavBarTitle:(NSString*)navBarTitleText heading:(NSString*)headingText body:(NSString*)bodyText
{
	self = [super init];
	if (self)
    {
        self.navBarTitle = navBarTitleText;
        self.heading = headingText;
        self.body = bodyText;
	}
	return self;
}

@end
