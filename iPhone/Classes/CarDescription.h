//
//  CarDescription.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarDescription : NSObject
{
	NSString	*carAC;
	NSString	*carBody;
	NSString	*carClass;
	NSString	*carCode;
	NSString	*carFuel;
	NSString	*carTrans;
}

@property (nonatomic, strong) NSString*	carAC;
@property (nonatomic, strong) NSString*	carBody;
@property (nonatomic, strong) NSString*	carClass;
@property (nonatomic, strong) NSString*	carCode;
@property (nonatomic, strong) NSString*	carFuel;
@property (nonatomic, strong) NSString*	carTrans;

@end
