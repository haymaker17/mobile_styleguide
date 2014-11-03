#import "MOFBHelper.h"

@implementation MOFBHelper

@synthesize delegate;

- (id) init {
	self = [super init];
	mofbStatus = [[[MOFBStatus alloc] init] retain];
	mofbStatus.delegate = self;		
	return self;
}

- (NSString *)status {
	return mofbStatus.status;
}

- (void)setStatus:(NSString *)status {
	[mofbStatus update:status];
}

- (void)statusDidUpdate:(MOFBStatus*)aMofbStatus {
	[delegate statusDidUpdate:self];
}

-(void)status:(MOFBStatus*)aMofbStatus DidFailWithError:(NSError*)error {
	[delegate status:self DidFailWithError:error];
}

- (void)dealloc {
	[mofbStatus release];
	[super dealloc];
}

@end

