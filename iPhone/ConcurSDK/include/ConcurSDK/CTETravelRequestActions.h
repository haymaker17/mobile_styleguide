//
//  CTETravelRequestActions.h
//  ConcurSDK
//
//  Created by laurent mery on 14/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
@class CTEUserAction, CTEError;

@interface CTETravelRequestActions : NSObject


/*
 * send action to the server
 */
-(void)action:(CTEUserAction *)cteUserAction success:(void (^)(NSString *responseObject))success failure:(void (^)(NSString	*errorMessage))failure;


@end
