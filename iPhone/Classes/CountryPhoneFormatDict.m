//
//  CountryPhoneFormatDict.m
//  ConcurMobile
//
//  Created by Ray Chi on 12/11/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CountryPhoneFormatDict.h"

@implementation CountryPhoneFormatDict

-(id)init
{
    self = [super init];
    //
    // The dictionary support most frequent 5 regions we support,
    // if here are more than 10 regions in the future
    NSArray *usPhoneFormats = [NSArray arrayWithObjects:
                               @"+1 (###) ###-####",
                               @"1 (###) ###-####",
                               @"011 $",
                               @"###-####",
                               @"(###) ###-####", nil];
    
    NSArray *caPhoneFormats = [NSArray arrayWithObjects:
                               @"+1 (###) ###-####",
                               @"1 (###) ###-####",
                               @"011 $",
                               @"###-####",
                               @"(###) ###-####", nil];
    
    NSArray *ukPhoneFormats = [NSArray arrayWithObjects:
                               @"+44 ##########",
                               @"00 $",
                               @"#######",
                               @"(01#1) ### ####",
                               @"(011#) ### ####",
                               @"(01###) ######",
                               @"(02#) #### ####",
                               @"07### ######",
                               @"0500 ######",
                               @"08## ######",nil];
    
    NSArray *jpPhoneFormats = [NSArray arrayWithObjects:
                               @"+81 ############",
                               @"001 $",
                               @"(0#) #######",
                               @"(0#) #### ####", nil];
    
    NSArray *inPhoneFormats = [NSArray arrayWithObjects:
                               @"0###-#######",
                               @"+91 #####-#####",
                               @"#####-#####"
                               @"",nil];
    
    self = (CountryPhoneFormatDict*)[[NSDictionary alloc] initWithObjectsAndKeys:
                                       caPhoneFormats, @"CA",
                                       usPhoneFormats, @"US",
                                       ukPhoneFormats, @"GB",
                                       jpPhoneFormats, @"JP",
                                       inPhoneFormats, @"IN",
                                       nil];
    return self;
}

@end
