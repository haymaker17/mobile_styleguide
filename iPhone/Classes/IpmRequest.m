//
//  IpmRequest.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "IpmRequest.h"
#import "IpmMessageParser.h"
#import "CTENetworking.h"

@interface IpmRequest()
@property (nonatomic, strong) CTENetworking *network;
@property (nonatomic, strong) NSString *target;
@end

@implementation IpmRequest

-(id)init
{
    self = [super init];
    if (self) {
        self.network = [[CTENetworking alloc] init];
    }
    return self;
}

-(id)initWithTarget:(NSString *)target
{
    self = [self init];
    if (self) {
        self.target = target;
    }
    return self;
}

-(void)requestIpmMessagesWithSuccess:(void (^) (NSArray *messages))success failure:(void (^)(CTEError *error))failure
{
    [self.network getXMLFromURL:self.serviceURL success:^(NSString *response){
        IpmMessageParser *ipmMsg = [[IpmMessageParser alloc] initWithXmlResponse:response];
        if (success) {
            success(ipmMsg.messages);
        }
    } failure:^(CTEError *error) {
        if (failure) {
            failure(error);
        }
    }];
}

- (NSString *)serviceURL
{
    NSString *url = [NSString stringWithFormat:@"/mobile/ipm/getmsg?target=%@",
                     self.target];
    
    return url;
}
@end
