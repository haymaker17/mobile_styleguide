//
//  CCViewController.m
//  ConcurMobile
//
//  Created by laurent mery on 20/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCViewController.h"



@implementation CCViewController


//TO BE OVERRIDED
+(NSString*)viewName{
	
	return @"undefined";
}

-(NSString*)viewName{
	
	return [[self class] viewName];
}


@end
