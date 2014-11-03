//
//  LoginHelpTopic.h
//  ConcurMobile
//
//  Created by charlottef on 12/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface LoginHelpTopic : NSObject
{
    NSString *navBarTitle;
    NSString *heading;
    NSString *body;
}

@property (strong, nonatomic) NSString* navBarTitle;
@property (strong, nonatomic) NSString* heading;
@property (strong, nonatomic) NSString* body;

+(NSArray*) topics;

-(id) initWithNavBarTitle:(NSString*)navBarTitleText heading:(NSString*)headingText body:(NSString*)bodyText;

@end
